// IBookManager.aidl
package com.willkernel.aidlbook;
// Declare any non-default types here with import statements
import  com.willkernel.aidlbook.Book;
import  com.willkernel.aidlbook.INewBookArrivedListener;
// AIDL 接口中只支持方法，不支持静态常量，区别于传统的接口
interface IBookManager {
    List<Book> getBookList();
    // AIDL 中除了基本数据类型，其他数据类型必须标上方向,in,out 或者 inout
    // in 表示输入型参数
    // out 表示输出型参数
    // inout 表示输入输出型参数
    void addBook(in Book book);

    void registerListener(INewBookArrivedListener listener);
    void unregisterListener(INewBookArrivedListener listener);
}
