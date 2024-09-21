package com.effacy.jui.test.description;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.SOURCE)
@Repeatable(TestDescriptions.class)
public @interface TestDescription {

    public String summary();
}
