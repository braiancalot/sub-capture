# Finding: landscape video is captured at 45% resolution

Status: historical. Confirmed and fixed on 2026-10-10 (`v1.1.1`), then made moot the same day:
`v2.0.0` removed screen capture and OCR. Kept for the record of what OCR got wrong. Everything
under Evidence describes the behavior before the fix.

## Evidence

Device screen is 1220x2712. Five frames captured in VLC, all saved as 1220x2712 (portrait):

- Phone held in portrait: the video is a 1220 px wide strip in the middle of the frame.
- Phone held in landscape: the whole 2712x1220 screen is scaled down to 1220x549 and letterboxed
  into the portrait buffer. The video inside it is about 976 px wide, against about 2170 px on the
  real screen. The rest of the buffer is transparent.

So a fullscreen landscape video reaches OCR with less resolution than the same video played in
portrait.

Cause: `ScreenFrameGrabber` sizes the `ImageReader` and the `VirtualDisplay` from `displayMetrics`
when `OverlayService` starts. `MainActivity` is locked to portrait, so those dimensions are always
portrait, and `VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR` fits a rotated screen inside them.

## Impact seen so far

OCR read the subtitle correctly in all five frames, including the landscape one. The subtitles were
large white text with a dark outline. Smaller or lower contrast subtitles have not been tested at
this resolution.

## Fix

`ScreenFrameGrabber` implements `MediaProjection.Callback.onCapturedContentResize`, which Android 14
calls when the screen rotates, and swaps in an `ImageReader` of the new size. Verified on the
device: the same landscape scene is now captured as 2712x1220 with the picture filling the frame.

Side effect seen in the first landscape capture after the fix: ML Kit split the subtitle into two
blocks, so the line came out as `... LUNCH MONEY? / MAAM, YES, MAAM.` where the 45% frame had given
one block. More pixels changes how blocks are grouped.

Rotating back to portrait and then to landscape again was also verified (1220x2712, then
2712x1220). Starting the capture in landscape cannot happen: `MainActivity` is locked to portrait.

## Related: stray blocks in the result

The landscape frame produced `A / WHAT ABOUT THE ELECTRICIAN? DID YOU GIVE HIM A CHECK?`. The same
scene in portrait produced no `A`. Every block ML Kit finds is joined with " / ", so anything it
reads as text ends up in the line. The source of the `A` is not identified: in that frame the
floating button sat over the picture instead of over a black bar, which makes it a suspect, and so
is the scenery. Logging each block with its bounding box would settle it.

## Related, still unverified

A frame is only delivered when the screen redraws. Over a paused video the button's pulse animation
is probably what produces the frame that `grabFrame` waits for.

## How to reproduce

Install the debug build, capture, then `adb pull
/sdcard/Android/data/br.com.teshi.subcapture.debug/files/frames .`. The frames are not kept in the
repository: they are stills from copyrighted video and the repository is public.
