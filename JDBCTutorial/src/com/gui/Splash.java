package com.gui;
import java.awt.Color;

import com.thehowtotutorial.splashscreen.JSplash;

public class Splash {

	public static void main(String[] args) {
		try {
			JSplash splash = new JSplash(Splash.class.getResource("Capture.png"),true, true, false, "V1", null, Color.red,Color.black);
			splash.splashOn();
			splash.setProgress(20, "init");
			Thread.sleep(1000);
			splash.setProgress(40, "loading");
			Thread.sleep(1000);
			splash.setProgress(60, "applying configs");
			Thread.sleep(1000);
			splash.setProgress(80, "starting app");
			Thread.sleep(1000);
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
