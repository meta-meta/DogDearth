package com.generalprocessingunit.processing;

import com.generalprocessingunit.processing.space.EuclideanSpaceObject;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import com.wolfram.alpha.*;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PShape;
import processing.core.PVector;

import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

public class WolframBar extends EuclideanSpaceObject {
    private static WolframBar instance;
    private boolean focused = false;
    private static boolean isWorking = false;
    private static PShape spikey;

    public static char[] charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$%^&*()-=_+/\\,.<>'\";:[]{}|`~?".toCharArray();
    private static Set<Character> chars = new HashSet<>();
    static {
        for(char c: charset) {
            chars.add(c);
        }

        setupTTS();
    }

    private static String queryString = "";

    private WolframBar() {}

    public static WolframBar getInstance() {
        if(null == instance) {
            instance = new WolframBar();
        }
        return instance;
    }

    public static void setFocus() {
        getInstance().focused = true;
    }

    public static boolean isFocused() {
        return getInstance().focused;
    }

    public static void setBarLocation(PVector loc) {
        getInstance().setLocation(loc);
    }

    static float w = 100, h = 10, d = 1, outerRing = h * .25f, midRing = h * .15f, innerRing = h * .05f;
    static float spikeyRotation = 0;
    public static void draw(PGraphics pG, PApplet p5) {
        if(null == spikey) {
            spikey = p5.loadShape("spikey.obj");
            spikey.scale(.01f);
        }

        getInstance().pushMatrixAndTransform(pG);
        {
            pG.colorMode(RGB);

            pG.pushMatrix();
            {
                pG.translate(-50, 15, 0);
                if(isWorking) {
                    spikeyRotation += .01f;
                }
                pG.rotateY(spikeyRotation);

                spikey.setFill(pG.color(255, 0, 0));
                spikey.setEmissive(pG.color(isWorking ? (127 + 127 * sin(p5.millis() / 1000f)) : 0, 0, 0));
                pG.shape(spikey);
            }
            pG.popMatrix();

            pG.noStroke();
            pG.fill(255, 170, 4);
            pG.emissive(255, 170, 4);
            pG.rect(-w/2 - outerRing, -h / 2 - outerRing, 2 * outerRing + w, 2 * outerRing + h, 1);

            pG.fill(254, 190, 110);
            pG.emissive(254, 190, 110);
            pG.pushMatrix();
            {
                pG.translate(0, 0, -.2f);
                pG.rect(-w/2 - midRing, -h / 2 - midRing, 2 * midRing + w, 2 * midRing + h, 1);
            }
            pG.popMatrix();

            pG.fill(250, 104, 0);
            pG.emissive(250, 104, 0);
            pG.pushMatrix();
            {
                pG.translate(0, 0, -.4f);
                pG.rect(-w/2 - innerRing, -h / 2 - innerRing, 2 * innerRing + w, 2 * innerRing + h, 1);
            }
            pG.popMatrix();

            pG.fill(255);
            pG.emissive(255);
            pG.box(w, h, d);

            pG.rotateX(PI);
            pG.textMode(SHAPE);
            pG.textAlign(LEFT);
            pG.fill(0);
            pG.emissive(0);

            String str = queryString;
            while(pG.textWidth(str + "|") > w * .9f) {
                str = str.substring(1, str.length());
            }
            pG.text(str + (p5.millis() % 1000 < 500 && isFocused() ? "|" : ""), -w/2, 2.5f, 5);
        }
        pG.popMatrix();
    }

    public static void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if(keyCode == KeyEvent.VK_ENTER) {
            // search
            getInstance().focused = false;
            if(!queryString.isEmpty()) {
                query();
            }
        } else if(keyCode == KeyEvent.VK_BACK_SPACE) {
            if(queryString.length() > 0) {
                queryString = queryString.substring(0, queryString.length() - 1);
            }
        } else if( chars.contains(e.getKeyChar())) {
            queryString += e.getKeyChar();
        } else if(keyCode == KeyEvent.VK_SPACE) {
            queryString += " ";
        }
    }

    private static class WolfQuery implements Runnable {
        @Override
        public void run() {
            isWorking = true;
            WAEngine engine = new WAEngine();

            engine.setAppID("68YAY3-THXTYRK7W8");
            engine.addFormat("plaintext");

            WAQuery query = engine.createQuery();

            query.setInput(queryString);

            try{
                WAQueryResult queryResult = engine.performQuery(query);

                if (queryResult.isError()) {
                    System.out.println("Query error");
                    System.out.println("  error code: " + queryResult.getErrorCode());
                    System.out.println("  error message: " + queryResult.getErrorMessage());
                } else if (!queryResult.isSuccess()) {
                    System.out.println("Query was not understood; no results available.");
                } else {
                    // Got a result.
                    System.out.println("Successful query. Pods follow:\n");
                    for (WAPod pod : queryResult.getPods()) {
                        if (!pod.isError()) {
                            voice.speak(pod.getTitle());
                            for (WASubpod subpod : pod.getSubpods()) {
                                for (Object element : subpod.getContents()) {
                                    if (element instanceof WAPlainText) {
                                        voice.speak(((WAPlainText) element).getText().replace("|", ";"));
                                    }
                                }
                            }
                            System.out.println("");
                        }
                    }
                    // We ignored many other types of Wolfram|Alpha output, such as warnings, assumptions, etc.
                    // These can be obtained by methods of WAQueryResult or objects deeper in the hierarchy.
                }

            } catch (WAException e) {
                e.printStackTrace();
            }

            voice.speak("That is all.");
            isWorking = false;
        }
    }

    private static void query() {
        Thread thread = new Thread(new WolfQuery());
        thread.start();
    }

    private static Voice voice;
    private static void setupTTS() {
         /* The VoiceManager manages all the voices for FreeTTS.
         */
        VoiceManager voiceManager = VoiceManager.getInstance();
        voice = voiceManager.getVoice("kevin16");


        /* Allocates the resources for the voice.
         */
        voice.allocate();
    }

    public static void cleanupVoice() {
        /* Clean up and leave.
         */
        voice.deallocate();
    }
}
