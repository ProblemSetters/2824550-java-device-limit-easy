package com.devices;

import com.devices.Models.CommandResult;
import com.devices.Models.Plan;
import com.devices.Models.Reason;
import com.devices.Models.Sessions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandsTest {

    private static void assertOk(CommandResult result, String expectedMessage) {
        assertTrue(result.ok, () -> "expected ok with \"" + expectedMessage + "\", got ok=" + result.ok + " reason=" + result.reason);
        assertEquals(expectedMessage, result.message);
    }

    private static void assertError(CommandResult result, Reason expectedReason) {
        assertFalse(result.ok, () -> "expected error " + expectedReason + ", got ok=" + result.ok + " message=\"" + result.message + "\"");
        assertEquals(expectedReason, result.reason);
    }

    private static void assertSessionsJson(Sessions sessions, String expectedJson) {
        assertEquals(expectedJson, FileIo.sessionsToJson(sessions));
    }

    private static Plan makePlan(String planDoc) {
        return Parser.parsePlanJson(planDoc);
    }

    private static Sessions makeSessions(String sessionsDoc) {
        return Parser.parseSessionsJson(sessionsDoc);
    }

    @Test
    @DisplayName("login signs a device in and reports the new count")
    public void test_login_signs_a_device_in_and_reports_the_new_count() {
        Plan premium = makePlan("""
{
  "plan":"Premium",
  "device_limit":4
}""");
        Sessions household = makeSessions("""
{
  "active":["phone","tv"]
}""");

        CommandResult capitals = Commands.login(household, premium, "TV");      // two of four in use
        CommandResult lastSlot = Commands.login(household, premium, "laptop");  // three of four in use, so this fills the plan

        assertOk(capitals, "TV signed in (3/4)");
        assertOk(lastSlot, "laptop signed in (4/4)");

        assertSessionsJson(household, """
{
  "active":["phone","tv","TV","laptop"]
}""");

        Plan business = makePlan("""
{
  "plan":"Business",
  "device_limit":60
}""");
        Sessions office = makeSessions("""
{
  "active":["desktop"]
}""");

        CommandResult second = Commands.login(office, business, "tablet");  // one of sixty in use

        assertOk(second, "tablet signed in (2/60)");

        assertSessionsJson(office, """
{
  "active":["desktop","tablet"]
}""");
    }

    @Test
    @DisplayName("login and switch rejections use the stated reason")
    public void test_login_and_switch_rejections_use_the_stated_reason() {
        Plan standard = makePlan("""
{
  "plan":"Standard",
  "device_limit":2
}""");
        Sessions household = makeSessions("""
{
  "active":["phone","tv"]
}""");

        CommandResult repeat = Commands.login(household, standard, "phone");       // phone is already signed in
        CommandResult noRoom = Commands.login(household, standard, "tablet");     // new, and both slots are taken
        CommandResult unknownOld = Commands.switchDevice(household, standard, "laptop", "tablet");    // laptop is not signed in
        CommandResult unknownOldTakenNew = Commands.switchDevice(household, standard, "laptop", "tv");  // laptop is out, tv is in
        CommandResult takenNew = Commands.switchDevice(household, standard, "phone", "tv");        // tv is already signed in

        assertError(repeat, Reason.ALREADY_LOGGED_IN);
        assertError(noRoom, Reason.DEVICE_LIMIT);
        assertError(unknownOld, Reason.NOT_LOGGED_IN);
        assertError(unknownOldTakenNew, Reason.NOT_LOGGED_IN);
        assertError(takenNew, Reason.ALREADY_LOGGED_IN);

        assertSessionsJson(household, """
{
  "active":["phone","tv"]
}""");

        Plan family = makePlan("""
{
  "plan":"Family",
  "device_limit":3
}""");
        Sessions familyHome = makeSessions("""
{
  "active":["tv","phone","laptop"]
}""");

        CommandResult familyRepeat = Commands.login(familyHome, family, "phone");      // phone is already signed in
        CommandResult familyNoRoom = Commands.login(familyHome, family, "console");   // new, and all three slots are taken

        assertError(familyRepeat, Reason.ALREADY_LOGGED_IN);
        assertError(familyNoRoom, Reason.DEVICE_LIMIT);

        assertSessionsJson(familyHome, """
{
  "active":["tv","phone","laptop"]
}""");

        Plan premium = makePlan("""
{
  "plan":"Premium",
  "device_limit":4
}""");
        Sessions roomyHome = makeSessions("""
{
  "active":["phone","tv"]
}""");

        CommandResult roomyRepeat = Commands.login(roomyHome, premium, "phone");  // phone is already signed in

        assertError(roomyRepeat, Reason.ALREADY_LOGGED_IN);

        assertSessionsJson(roomyHome, """
{
  "active":["phone","tv"]
}""");
    }

    @Test
    @DisplayName("logout frees the slot and keeps the order")
    public void test_logout_frees_the_slot_and_keeps_the_order() {
        Plan premium = makePlan("""
{
  "plan":"Premium",
  "device_limit":4
}""");
        Sessions household = makeSessions("""
{
  "active":["phone","tv","kids tablet","laptop"]
}""");

        CommandResult middle = Commands.logout(household, premium, "tv");  // four of four in use

        assertOk(middle, "tv signed out (3/4)");

        assertSessionsJson(household, """
{
  "active":["phone","kids tablet","laptop"]
}""");

        CommandResult first = Commands.logout(household, premium, "phone");     // three of four in use
        CommandResult again = Commands.logout(household, premium, "tv");        // already signed out
        CommandResult never = Commands.logout(household, premium, "console");   // never signed in

        assertOk(first, "phone signed out (2/4)");
        assertError(again, Reason.NOT_LOGGED_IN);
        assertError(never, Reason.NOT_LOGGED_IN);

        assertSessionsJson(household, """
{
  "active":["kids tablet","laptop"]
}""");
    }

    @Test
    @DisplayName("switch replaces the device in its place")
    public void test_switch_replaces_the_device_in_its_place() {
        Plan premium = makePlan("""
{
  "plan":"Premium",
  "device_limit":4
}""");
        Sessions household = makeSessions("""
{
  "active":["phone","tv","work laptop"]
}""");

        CommandResult first = Commands.switchDevice(household, premium, "phone", "tablet");  // three of four in use

        assertOk(first, "Switched phone to tablet (3/4)");

        assertSessionsJson(household, """
{
  "active":["tablet","tv","work laptop"]
}""");

        CommandResult middle = Commands.switchDevice(household, premium, "tv", "console");  // still three of four in use

        assertOk(middle, "Switched tv to console (3/4)");

        assertSessionsJson(household, """
{
  "active":["tablet","console","work laptop"]
}""");
    }

    @Test
    @DisplayName("switch works when the plan is full")
    public void test_switch_works_when_the_plan_is_full() {
        Plan standard = makePlan("""
{
  "plan":"Standard",
  "device_limit":2
}""");
        Sessions household = makeSessions("""
{
  "active":["phone","tv"]
}""");

        CommandResult pairSwap = Commands.switchDevice(household, standard, "tv", "tablet");  // two of two in use

        assertOk(pairSwap, "Switched tv to tablet (2/2)");

        assertSessionsJson(household, """
{
  "active":["phone","tablet"]
}""");

        Plan family = makePlan("""
{
  "plan":"Family",
  "device_limit":3
}""");
        Sessions familyHome = makeSessions("""
{
  "active":["tv","phone","laptop"]
}""");

        CommandResult trioSwap = Commands.switchDevice(familyHome, family, "laptop", "console");  // three of three in use

        assertOk(trioSwap, "Switched laptop to console (3/3)");

        assertSessionsJson(familyHome, """
{
  "active":["tv","phone","console"]
}""");
    }

    @Test
    @DisplayName("switch refuses a device switched for itself")
    public void test_switch_refuses_a_device_switched_for_itself() {
        Plan standard = makePlan("""
{
  "plan":"Standard",
  "device_limit":2
}""");
        Sessions household = makeSessions("""
{
  "active":["phone","tv"]
}""");

        CommandResult toItself = Commands.switchDevice(household, standard, "phone", "phone");    // same name twice
        CommandResult unknownTwice = Commands.switchDevice(household, standard, "laptop", "laptop");  // same name, and not signed in

        assertError(toItself, Reason.SAME_DEVICE);
        assertError(unknownTwice, Reason.SAME_DEVICE);

        assertSessionsJson(household, """
{
  "active":["phone","tv"]
}""");
    }
}
