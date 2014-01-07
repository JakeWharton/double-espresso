package com.google.android.apps.common.testing.ui.espresso.action;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.MockitoAnnotations.initMocks;

import android.view.View;

import junit.framework.TestCase;

import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Unit tests for {@link GeneralLocation}.
 */
public class GeneralLocationTest extends TestCase {

  private static final int VIEW_POSITION_X = 100;
  private static final int VIEW_POSITION_Y = 50;
  private static final int VIEW_WIDTH = 150;
  private static final int VIEW_HEIGHT = 300;

  private static final int AXIS_X = 0;
  private static final int AXIS_Y = 1;

  @Spy
  private View mockView;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    initMocks(this);
    
    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        int[] array = (int[]) invocation.getArguments()[0];
        array[AXIS_X] = VIEW_POSITION_X;
        array[AXIS_Y] = VIEW_POSITION_Y;
        return null;
      }
    }).when(mockView).getLocationOnScreen(any(int[].class));

    mockView.layout(
            VIEW_POSITION_X,
            VIEW_POSITION_Y,
            VIEW_POSITION_X + VIEW_WIDTH,
            VIEW_POSITION_Y + VIEW_HEIGHT);
  }

  public void testLeftLocationsX() {
      assertPositionEquals(VIEW_POSITION_X, GeneralLocation.TOP_LEFT, AXIS_X);
      assertPositionEquals(VIEW_POSITION_X, GeneralLocation.CENTER_LEFT, AXIS_X);
      assertPositionEquals(VIEW_POSITION_X, GeneralLocation.BOTTOM_LEFT, AXIS_X);
  }

  public void testRightLocationsX() {
    assertPositionEquals(VIEW_POSITION_X + VIEW_WIDTH, GeneralLocation.TOP_RIGHT, AXIS_X);
    assertPositionEquals(VIEW_POSITION_X + VIEW_WIDTH, GeneralLocation.CENTER_RIGHT, AXIS_X);
    assertPositionEquals(VIEW_POSITION_X + VIEW_WIDTH, GeneralLocation.BOTTOM_RIGHT, AXIS_X);
  }

  public void testTopLocationsY() {
    assertPositionEquals(VIEW_POSITION_Y, GeneralLocation.TOP_LEFT, AXIS_Y);
    assertPositionEquals(VIEW_POSITION_Y, GeneralLocation.TOP_CENTER, AXIS_Y);
    assertPositionEquals(VIEW_POSITION_Y, GeneralLocation.TOP_RIGHT, AXIS_Y);
  }

  public void testBottomLocationsY() {
    assertPositionEquals(VIEW_POSITION_Y + VIEW_HEIGHT, GeneralLocation.BOTTOM_LEFT, AXIS_Y);
    assertPositionEquals(VIEW_POSITION_Y + VIEW_HEIGHT, GeneralLocation.BOTTOM_CENTER, AXIS_Y);
    assertPositionEquals(VIEW_POSITION_Y + VIEW_HEIGHT, GeneralLocation.BOTTOM_RIGHT, AXIS_Y);
  }

  public void testCenterLocationsX() {
    assertPositionEquals(VIEW_POSITION_X + VIEW_WIDTH / 2, GeneralLocation.CENTER, AXIS_X);
    assertPositionEquals(VIEW_POSITION_X + VIEW_WIDTH / 2, GeneralLocation.TOP_CENTER, AXIS_X);
    assertPositionEquals(VIEW_POSITION_X + VIEW_WIDTH / 2, GeneralLocation.BOTTOM_CENTER, AXIS_X);
  }

  public void testCenterLocationsY() {
    assertPositionEquals(VIEW_POSITION_Y + VIEW_HEIGHT / 2, GeneralLocation.CENTER, AXIS_Y);
    assertPositionEquals(VIEW_POSITION_Y + VIEW_HEIGHT / 2, GeneralLocation.CENTER_LEFT, AXIS_Y);
    assertPositionEquals(VIEW_POSITION_Y + VIEW_HEIGHT / 2, GeneralLocation.CENTER_RIGHT, AXIS_Y);
  }

  private void assertPositionEquals(int expected, GeneralLocation location, int axis) {
    assertEquals(expected, location.calculateCoordinates(mockView)[axis], 0.1f);
  }

}
