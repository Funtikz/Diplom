package org.example.ui.annotations.video;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RecordVideo {
    boolean value() default true;
    String dir() default "build/video-results";
    int width() default 1920;
    int height() default 1080;
}