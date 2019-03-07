// INewBookArrivedListener.aidl
package com.willkernel.aidlbook;

// Declare any non-default types here with import statements
import  com.willkernel.aidlbook.Book;
interface INewBookArrivedListener {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void onNewBookArrived(in Book book);
}
