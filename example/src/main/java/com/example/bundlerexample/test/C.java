package com.example.bundlerexample.test;

import com.hendraanggrian.bundler.annotations.BindExtra;
import com.hendraanggrian.bundler.annotations.WrapExtras;

/**
 * Created by hendraanggrian on 5/3/17.
 */
@WrapExtras
public class C extends B {
    @BindExtra("c") String c;
}