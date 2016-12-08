package org.fabiomsr.yeko.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Fabiomsr on 25/7/16.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface SaveStated {
}
