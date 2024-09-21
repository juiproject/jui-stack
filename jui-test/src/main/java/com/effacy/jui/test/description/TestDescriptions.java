package com.effacy.jui.test.description;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD })
public @interface TestDescriptions {

    public TestDescription[] value();
}
