# Finding: landscape video may be captured at reduced resolution

Status: hypothesis from reading the code, not verified on a device.

## Evidence

- `ScreenFrameGrabber` sizes the `ImageReader` and the `VirtualDisplay` from `displayMetrics` at
  the moment `OverlayService` starts.
- `MainActivity` is locked to portrait, so those dimensions are always portrait (for example
  1080x2400).
- The video then plays in landscape. With `VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR` the landscape screen
  is expected to be scaled down and letterboxed into the portrait buffer, leaving the picture at
  under half its real width.

If true, OCR runs on a subtitle rendered at less than half the available resolution.

## How to verify

Save one captured frame to a PNG (or log `bitmap.width`, `bitmap.height` and the screen rotation)
while a landscape video is playing, and check whether the picture fills the frame.

## Possible fix

Recreate the `ImageReader` and call `VirtualDisplay.resize` when the display rotates, or size the
buffer to the larger dimension on both axes.

## Related

A frame is only delivered when the screen redraws. Over a paused video the button's pulse animation
is probably what produces the frame that `grabFrame` waits for. Also unverified.
