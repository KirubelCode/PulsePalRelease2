package com.movesense.showcaseapp.section_00_mainView;


import android.Manifest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.IdlingPolicies;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.idling.CountingIdlingResource;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiScrollable;
import androidx.test.uiautomator.UiSelector;
import androidx.recyclerview.widget.RecyclerView;
import android.test.suitebuilder.annotation.LargeTest;

import com.movesense.showcaseapp.R;
import com.movesense.showcaseapp.custom_matchers.RecyclerViewByTextNameMacher;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnHolderItem;
import static androidx.test.espresso.contrib.RecyclerViewActions.scrollToHolder;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class DFUTest {

    @Rule
    public ActivityTestRule<MainViewActivity> mActivityTestRule = new ActivityTestRule<>(MainViewActivity.class);
    private UiDevice mUiDevice;

    @Rule
    public GrantPermissionRule mGrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION);

    private final static String DFU_FILE_NAME = "Movesense-accelerometer-dfu.zip";
    private final static String DFU_MOVESENSE_DEVICE_NAME = "Movesense ECKICFD7F724";
    private CountingIdlingResource mConnectingIdlingResource;
    private CountingIdlingResource mDfuUploadIdlingResource;

    @Before
    public void setUp() {
        mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

//        mConnectingIdlingResource = DfuPresenter.getConnectionIdlingResource();
//        Espresso.registerIdlingResources(mConnectingIdlingResource);
//
//        mDfuUploadIdlingResource = DfuPresenter.getmDfuUploadIdlingResource();
//        Espresso.registerIdlingResources(mDfuUploadIdlingResource);

        IdlingPolicies.setIdlingResourceTimeout(5, TimeUnit.MINUTES);
        IdlingPolicies.setMasterPolicyTimeout(5, TimeUnit.MINUTES);
    }


    @After
    public void cleanUp() {

        if (mConnectingIdlingResource != null) {
            Espresso.unregisterIdlingResources(mConnectingIdlingResource);
        }

        if (mDfuUploadIdlingResource != null) {
            Espresso.unregisterIdlingResources(mDfuUploadIdlingResource);
        }
    }

    private void dfuTestLogic() throws UiObjectNotFoundException {
        // Click on DFU from main view
        ViewInteraction linearLayout = onView(
                allOf(withId(R.id.mainView_dfu_Ll), isDisplayed()));
        linearLayout.perform(click());

        // Click on fileSelection container
        ViewInteraction linearLayout2 = onView(
                allOf(withId(R.id.dfu_selectedFile_containerLl), isDisplayed()));
        linearLayout2.perform(click());

        // Click on DrawerIcon


        // Select file by UIAutomator
        UiScrollable fileList = new UiScrollable(new UiSelector().scrollable(true));
        fileList.scrollTextIntoView(DFU_FILE_NAME);
        mUiDevice.findObject(By.text(DFU_FILE_NAME)).click();

        // Click on deviceSelection container
        ViewInteraction linearLayout3 = onView(
                allOf(withId(R.id.dfu_selectedDevice_containerLl), isDisplayed()));
        linearLayout3.perform(click());

        // Wait for scan results
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Select device by name using custom matcher
        Matcher<RecyclerView.ViewHolder> matcher = RecyclerViewByTextNameMacher.withDfuHolderTimeView(DFU_MOVESENSE_DEVICE_NAME);
        onView((withId(R.id.device_list))).perform(scrollToHolder(matcher), actionOnHolderItem(matcher, click()));

        // Click on Proceed button
        ViewInteraction appCompatTextView = onView(
                allOf(withId(R.id.dfu_startUpload_btn), withText("Proceed"), isDisplayed()));
        appCompatTextView.perform(click());

        // Click Yes, Update on popup dialog for confirmation
        ViewInteraction appCompatButton = onView(
                allOf(withId(android.R.id.button1), withText("Yes, Update")));
        appCompatButton.perform(click());

    }

    @Test
    public void dFUTest1() throws UiObjectNotFoundException {
        dfuTestLogic();
    }

    @Test
    public void dFUTest2() throws UiObjectNotFoundException {
        dfuTestLogic();
    }

    @Test
    public void dFUTest3() throws UiObjectNotFoundException {
        dfuTestLogic();
    }

    @Test
    public void dFUTest4() throws UiObjectNotFoundException {
        dfuTestLogic();
    }

    @Test
    public void dFUTest5() throws UiObjectNotFoundException {
        dfuTestLogic();
    }
}
