package com.stealthcotper.networktools;

/**
 * Created by matthew on 20/12/16.
 */

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.WindowManager;

import com.squareup.spoon.Spoon;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    private MainActivity activity;

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    @Before
    public void setUp() {
        activity = mActivityRule.getActivity();

        // Code to wake up screen before running tests
        Runnable wakeUpDevice = new Runnable() {
            public void run() {
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        };
        activity.runOnUiThread(wakeUpDevice);
    }

    @Test
    public void checkPing() {

        Spoon.screenshot(mActivityRule.getActivity(), "ping");

        setUpText("google.com");

        // Click ping button
        onView(withId(R.id.pingButton)).perform(click());

        sleep();

        Spoon.screenshot(mActivityRule.getActivity(), "ping");
    }

    @Test
    public void checkWOL() {

        Spoon.screenshot(mActivityRule.getActivity(), "wake-on-lan");

        setUpText("localhost");

        // Click ping button
        onView(withId(R.id.wolButton)).perform(click());

        sleep();

        Spoon.screenshot(mActivityRule.getActivity(), "wake-on-lan");
    }

    @Test
    public void checkPortScan() {

        Spoon.screenshot(mActivityRule.getActivity(), "port_scan");

        setUpText("localhost");

        // Click ping button
        onView(withId(R.id.portScanButton)).perform(click());

        sleep();

        Spoon.screenshot(mActivityRule.getActivity(), "port_scan");
    }

//    @Test
//    public void checkGitHubButton(){
//        onView(withId(R.id.action_github)).perform(click());
//
//        intended(allOf(hasData(hasHost(equalTo("www.google.com"))),
//                hasAction(Intent.ACTION_VIEW)));
//    }

    private void setUpText(String hostNameOrIp){
        // Enter text
        onView(withId(R.id.editIpAddress))
                .perform(clearText(), typeText(hostNameOrIp), closeSoftKeyboard());

        // Check text is entered
        onView(withId(R.id.editIpAddress)).check(matches(withText(hostNameOrIp)));
    }

    private void sleep(){
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
