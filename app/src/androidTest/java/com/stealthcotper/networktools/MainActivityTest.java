package com.stealthcotper.networktools;

/**
 * Created by matthew on 20/12/16.
 */

import android.app.Activity;
import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.squareup.spoon.Spoon;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasData;
import static android.support.test.espresso.intent.matcher.UriMatchers.hasHost;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.intent.Intents.intended;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    @Test
    public void checkPing() {
        // Type text and then press the button.
        String hostName = "google.com";

        // Enter text
        onView(withId(R.id.editIpAddress))
                .perform(clearText(), typeText(hostName), closeSoftKeyboard());

        // Check text is entered
        onView(withId(R.id.editIpAddress)).check(matches(withText(hostName)));

        // Click ping button
        onView(withId(R.id.pingButton)).perform(click());

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Spoon.screenshot(mActivityRule.getActivity(), "ping");
    }

    @Test
    public void checkWOL() {
        // Type text and then press the button.
        String hostName = "localhost";

        // Enter text
        onView(withId(R.id.editIpAddress))
                .perform(clearText(), typeText(hostName), closeSoftKeyboard());

        // Check text is entered
        onView(withId(R.id.editIpAddress)).check(matches(withText(hostName)));

        // Click ping button
        onView(withId(R.id.wolButton)).perform(click());

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Spoon.screenshot(mActivityRule.getActivity(), "wake-on-lan");
    }

    @Test
    public void checkPortScan() {
        // Type text and then press the button.
        String hostName = "localhost";

        // Enter text
        onView(withId(R.id.editIpAddress))
                .perform(clearText(), typeText(hostName), closeSoftKeyboard());

        // Check text is entered
        onView(withId(R.id.editIpAddress)).check(matches(withText(hostName)));

        // Click ping button
        onView(withId(R.id.portScanButton)).perform(click());

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Spoon.screenshot(mActivityRule.getActivity(), "port_scan");
    }

//    @Test
//    public void checkGitHubButton(){
//        onView(withId(R.id.action_github)).perform(click());
//
//        intended(allOf(hasData(hasHost(equalTo("www.google.com"))),
//                hasAction(Intent.ACTION_VIEW)));
//    }
}
