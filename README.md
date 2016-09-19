# Demo

This is a simple app demonstrating a couple of neat features that I've recently worked on.

###Hearts
Tapping the FAB on the main activity will send a heart floating up the screen in a randomized sine wave pattern.
The heart adjusts it's angle relative to the wave's current slope, and fades out as it floats upwards. Hundreds of hearts
can be animated simultaneously.

###Camera Roll
In-app loading of the camera roll using the library [Glide](https://github.com/bumptech/glide) to show and cache thumbnails. Glide is used by Google 
in their Camera App and was recommended in Google IO 2014. I've combined this with a StaggeredGridLayoutManager RecyclerView, 
which uses preloading to give the visual effect of an infinite cache. Glide only had documented support of preloading
for ListViews, however I found some undocumented (and non-compiling) code on their unreleased master branch for RecyclerView
preloading which I modified to get working. To test memory management, I wrote another simple app that generates any number
of randomly sized images. Rapidly scrolling through over a thousand images takes up ~100mb of memory and is seamlessly smooth.

I wanted to make use of various Android APIs, so I added the ability to create and view albums of photos and videos. 

Some APIs I've used:
- SQLiteOpenHelper - Persists the Album info across application instances
- BroadcastReceiver - Communicate DB updates back to the UI
- CollapsingToolbarLayout - Fancy new toolbar functionality from Google's design library
- ItemTouchHelper - For reordering and deleting albums from the RecyclerView (long press to reorder, swipe right to delete)
- BottomSheetDialogFragment - For creating new albums
- MediaStore - To retrieve photos and videos from external storage
- SVG - Most assets I've included in the SVG format
- Multi Screen Support - Single pane for phones in portrait, multi-pane for phones in landscape and 7"+ tablets in any orientation

I've been careful to store all instance state properly, so the app survives activity recreation at any point in time (feel free 
to try it out with the developer setting "Don't keep activities" enabled!). 
