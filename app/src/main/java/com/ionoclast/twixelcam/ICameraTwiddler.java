// CameraTwixelator.java
// Camera preview and capture twixelator
// TwixelCam - Copyright © 2015-2016 Brigham Toskin


package com.ionoclast.twixelcam;

import android.hardware.Camera;


/**
 * @author btoskin &lt;brigham@ionoclast.com&gt; Ionoclast Laboratories, LLC.
 */
public interface ICameraTwiddler extends Camera.PreviewCallback, Camera.PictureCallback {
	void AttachCamera(Camera pCamera);
	void CleanUp();
	void OpenGallery();
}
