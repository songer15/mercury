package com.valor.mercury.executor.test


cfg = "abc"
bar = "def"
if (true) {
    baz = "ghi"
    this.binding.variables.each {k,v ->  println "$k = $v"}
    this.binding.properties.each {k,v ->  println "$k = $v"}
}

//def run(foo) {
//    println 'Hello World!'
//    int x = 123
//    foo * 10
//}
//
//run foo





