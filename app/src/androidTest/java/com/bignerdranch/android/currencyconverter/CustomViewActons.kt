package com.bignerdranch.android.currencyconverter

import android.view.View
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.util.HumanReadables
import androidx.test.espresso.util.TreeIterables
import org.hamcrest.Matcher
import java.util.concurrent.TimeoutException

/**
 * This ViewAction tells espresso to wait till a certain view is found in the view hierarchy.
 * @param viewId The id of the view to wait for.
 * @param timeout The maximum time which espresso will wait for the view to show up (in milliseconds)
 * https://www.repeato.app/espresso-wait-for-view/
 */
fun waitForView(viewId: Int, timeout: Long): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return isRoot()
        }

        override fun getDescription(): String {
            return "wait for a specific view with id $viewId; during $timeout millis."
        }

        override fun perform(uiController: UiController, rootView: View) {
            uiController.loopMainThreadUntilIdle()
            val startTime = System.currentTimeMillis()
            val endTime = startTime + timeout
            val viewMatcher = withId(viewId)

            do {
                // Iterate through all views on the screen and see if the view we are looking for is there already
                for (child in TreeIterables.breadthFirstViewTraversal(rootView)) {
                    // found view with required ID
                    if (viewMatcher.matches(child)) {
                        return
                    }
                }
                // Loops the main thread for a specified period of time.
                // Control may not return immediately, instead it'll return after the provided delay has passed and the queue is in an idle state again.
                uiController.loopMainThreadForAtLeast(100)
            } while (System.currentTimeMillis() < endTime) // in case of a timeout we throw an exception -&gt; test fails
            throw PerformException.Builder()
                .withCause(TimeoutException())
                .withActionDescription(this.description)
                .withViewDescription(HumanReadables.describe(rootView))
                .build()
        }
    }
}
///ЕСТЬ ЕЩЕ ПОДХОД ЧЕРЕЗ IdlingResource, вроде рекомендованный  гугл
//этот код может пригодиться для реализации этого подхода
//https://www.repeato.app/idling-resources-in-espresso/
//public class ContentLoadingIdlingResource implements IdlingResource {
//    private ResourceCallback resourceCallback;
//
//    private boolean isIdle;
//    @Override public String getName() {
//        return ContentLoadingIdlingResource.class.getName();
//    }
//
//    @Override public boolean isIdleNow() {
//        if (isIdle)
//            return true;
//        if (getCurrentActivity() == null)
//            return false;
//        ContentFragment f = (ContentFragment) getCurrentActivity().getFragmentManager().findFragmentByTag(ContentFragment.TAG);
//        isIdle = f == null;
//        if (isIdle) {
//            resourceCallback.onTransitionToIdle();
//        }
//        return isIdle;
//    }
//
//    public Activity getCurrentActivity() {
//        return ((TestApplication) InstrumentationRegistry.getTargetContext().getApplicationContext()).getCurrentActivity(); }
//
//    @Override public void registerIdleTransitionCallback( ResourceCallback resourceCallback) {
//        this.resourceCallback = resourceCallback;
//    }
//}