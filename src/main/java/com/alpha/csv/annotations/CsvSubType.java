//package com.alpha.csv.annotations;
//
//import java.lang.annotation.Documented;
//import java.lang.annotation.ElementType;
//import java.lang.annotation.Retention;
//import java.lang.annotation.RetentionPolicy;
//import java.lang.annotation.Target;
//
///**
// * This Annotation is used to do customization when serialize property
// * Customization: custom CSV header, custom serialization order.
// */
//@Target({ElementType.FIELD})
//@Retention(RetentionPolicy.RUNTIME)
//@Documented
//public @interface CsvSubType{
//    
//    CsvProperty csvProperty();
//    
//    /**
//     * Names of subType's properties, which should be serialized.
//     */
//    String[] value() default {};
//}
