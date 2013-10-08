package com.google.android.apps.common.testing.ui.espresso.util;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;

import com.google.android.apps.common.testing.ui.espresso.util.TreeIterables.DistanceRecordingTreeViewer;
import com.google.android.apps.common.testing.ui.espresso.util.TreeIterables.TreeViewer;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import junit.framework.TestCase;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Unit tests for {@link TreeIterables}. */
public class TreeIterablesTest extends TestCase {

  private static class TestElement {
    private final String data;
    private final ImmutableList<TestElement> children;
    public TestElement(String data, TestElement ... children) {
      this.data = checkNotNull(data);
      this.children = ImmutableList.copyOf(children);
    }
  }

  private static class TestElementTreeViewer implements TreeViewer<TestElement> {
    @Override
    public Collection<TestElement> children(TestElement element) {
      return element.children;
    }
  }

  private static class TestElementStringConvertor implements Function<TestElement, String> {
    @Override
    public String apply(TestElement e) {
      return e.data;
    }
  }


  private static final TestElement trivialTree =
      new TestElement("a", new TestElement("b", new TestElement("c", new TestElement("d"))));

  private static final TestElement complexTree =
      new TestElement("a",
        new TestElement("b",
          new TestElement("c",
            new TestElement("d"),
            new TestElement("e",
              new TestElement("f"))),
          new TestElement("g"),
          new TestElement("h",
            new TestElement("i",
              new TestElement("j",
                new TestElement("k"))))),
        new TestElement("l"),
        new TestElement("m"),
        new TestElement("n",
                        new TestElement("o",
            new TestElement("p"),
            new TestElement("q"))));

  public void testDistanceRecorder_unknownItemThrowsException() {
    final DistanceRecordingTreeViewer<TestElement> distanceRecorder =
        new DistanceRecordingTreeViewer<TestElement>(complexTree, new TestElementTreeViewer());
    try {
      distanceRecorder.getDistance(new TestElement("hello"));
      fail("node should be unknown");
    } catch (RuntimeException expected) { }
  }

  public void testDistanceRecorder_unprocessedChildThrowsException() {
    final DistanceRecordingTreeViewer<TestElement> distanceRecorder =
        new DistanceRecordingTreeViewer<TestElement>(complexTree, new TestElementTreeViewer());

    try {
      distanceRecorder.getDistance(complexTree.children.iterator().next());
      fail("distance recorder hasnt processed this child yet, cannot know distance");
    } catch (RuntimeException expected) { }
  }

  public void testDistanceRecorder_distanceKnownAfterChildrenCall() {
    final DistanceRecordingTreeViewer<TestElement> distanceRecorder =
        new DistanceRecordingTreeViewer<TestElement>(complexTree, new TestElementTreeViewer());

    @SuppressWarnings("unused")
    List<TestElement> createdForSideEffect = Lists.newArrayList(
        distanceRecorder.children(complexTree));

    assertThat(distanceRecorder.getDistance(complexTree), is(0));
    assertThat(distanceRecorder.getDistance(complexTree.children.iterator().next()), is(1));
  }

  @SuppressWarnings("unchecked")
  public void testComplexTree_Distances() {
    final DistanceRecordingTreeViewer<TestElement> distanceRecorder =
        new DistanceRecordingTreeViewer<TestElement>(complexTree, new TestElementTreeViewer());
    Iterable<TestElement> complexIterable = TreeIterables.depthFirstTraversal(complexTree,
        distanceRecorder);
    Set<TestElement> complexSet = Sets.newHashSet(complexIterable);
    Map<String, Integer> distancesByData = Maps.newHashMap();
    for (TestElement e : complexSet) {
      distancesByData.put(e.data, distanceRecorder.getDistance(e));
    }

    assertThat(distancesByData, allOf(
        hasEntry("a", 0),
        hasEntry("b", 1),
        hasEntry("c", 2),
        hasEntry("d", 3),
        hasEntry("e", 3),
        hasEntry("f", 4),
        hasEntry("g", 2),
        hasEntry("h", 2),
        hasEntry("i", 3),
        hasEntry("j", 4),
        hasEntry("k", 5),
        hasEntry("l", 1),
        hasEntry("m", 1),
        hasEntry("n", 1),
        hasEntry("o", 2),
        hasEntry("p", 3),
        hasEntry("q", 3)));
    assertThat(distancesByData.size(), is(17));

    List<String> traversalOrder = Lists.newArrayList(Iterables.transform(
        complexIterable,
        new TestElementStringConvertor()));

    // should be depth first if forwarding correctly.
    assertThat(traversalOrder,
        is((List<String>) Lists.newArrayList(
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q")));
  }

  public void testComplexTraversal_depthFirst() {
    List<String> breadthFirst = Lists.newArrayList(Iterables.transform(
        TreeIterables.depthFirstTraversal(complexTree, new TestElementTreeViewer()),
        new TestElementStringConvertor()));
    assertThat(breadthFirst,
        is((Iterable<String>) Lists.newArrayList(
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q")));
  }

  public void testComplexTraversal_breadthFirst() {
    List<String> breadthFirst = Lists.newArrayList(Iterables.transform(
        TreeIterables.breadthFirstTraversal(complexTree, new TestElementTreeViewer()),
        new TestElementStringConvertor()));
    assertThat(breadthFirst,
        is((List<String>) Lists.newArrayList(
            "a", //root
            "b", "l", "m", "n", //L1
            "c", "g", "h", "o", //L2
            "d", "e", "i", "p", "q", //L3
            "f", "j", // L4
            "k"))); //L5
  }

  public void testTrivialTraversal_breadthFirst() {
    // essentially the same as depth first.
    List<String> breadthFirst = Lists.newArrayList(Iterables.transform(
        TreeIterables.breadthFirstTraversal(trivialTree, new TestElementTreeViewer()),
        new TestElementStringConvertor()));
    assertThat(breadthFirst, is((List<String>) Lists.newArrayList("a", "b", "c", "d")));
  }

  public void testTrivialTraversal_depthFirst() {
    List<String> depthFirst = Lists.newArrayList(Iterables.transform(
        TreeIterables.depthFirstTraversal(trivialTree, new TestElementTreeViewer()),
        new TestElementStringConvertor()));
    assertThat(depthFirst, is((List<String>) Lists.newArrayList("a", "b", "c", "d")));
  }

  @SuppressWarnings("unchecked")
  public void testTrivial_distance() {
    final DistanceRecordingTreeViewer<TestElement> distanceRecorder =
        new DistanceRecordingTreeViewer<TestElement>(trivialTree, new TestElementTreeViewer());

    Iterable<TestElement> trivialIterable = TreeIterables.depthFirstTraversal(trivialTree,
        distanceRecorder);
    Set<TestElement> trivialSet = Sets.newHashSet(trivialIterable);
    Map<String, Integer> distancesByData = Maps.newHashMap();
    for (TestElement e : trivialSet) {
      distancesByData.put(e.data, distanceRecorder.getDistance(e));
    }

    assertThat(distancesByData, allOf(
        hasEntry("a", 0),
        hasEntry("b", 1),
        hasEntry("c", 2),
        hasEntry("d", 3)));
    assertThat(distancesByData.size(), is(4));
  }
}
