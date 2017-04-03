BeanPurée
=========

**BeanPurée** is a middle layer between JavaBeans and [shapeless][shapeless].

> NOTE: The library is in active development stage. So the API might be changed.

[![Build Status](https://travis-ci.org/limansky/beanpuree.svg?branch=master)](https://travis-ci.org/limansky/beanpuree)
[![Join the chat at https://gitter.im/limansky/beanpuree](https://badges.gitter.im/limansky/beanpuree.svg)](https://gitter.im/limansky/beanpuree?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

## Motivation

Even though Scala is compatible with Java, the languages are different, and the
coding styles are different too.  In Scala we prefer to use immutable case classes,
but in Java world mutable JavaBeans are common building blocks.  Moreover, many
Scala libraries provide API which requires case classes (e.g. different serializers).
As result, we need to have similar model classes for Java and Scala.  This library
helps to convert data between JavaBeans and case classes.

## Usage

BeanPurée is available for Scala 2.10, 2.11 and 2.12.  Currently there is no
release version.  Adding in `sbt`:

```Scala
libraryDependencies += "me.limansky" %% "beanpuree" % "0.1"
```

If you'd like to use development version:

```Scala
resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies += "me.limansky" %% "beanpuree" % "0.2-SNAPSHOT"
```

The core of BeanPurée is `BeanGeneric` class, which have a same role with
shapeless `Generic`, but for JavaBeans.  Assume we have class:

```Java
public class Foo {
    private int a;
    private String b;

    public int getA() { return a; }
    public void setA(int a) { this.a = a; }

    public String getB() { return b; }
    public void setB(String b) { this.b = b; }

    public String toString() {
        return "Foo(" + a + ", \"" + b + "\")";
    }
}

```

Now we can create `BeanGeneric` instance and convert bean to HList and backward:

```Scala
scala> val gen = BeanGeneric[Foo]
gen: me.limansky.beanpuree.BeanGeneric[Foo]{type Repr = shapeless.::[Int,shapeless.::[String,shapeless.HNil]]}

scala> val foo = gen.from(5 :: "aaa" :: HNil)
foo: Foo = Foo(5, "aaa")

scala> foo.setB("changed")

scala> gen.to(foo)
res2: gen.Repr = 5 :: changed :: HNil
```

Another important thing is `LabelledBeanGeneric`, which is a `LabelledGeneric` adopted
for the beans.  It's important to note, that it uses "field names" generated from the
getters names.  E.g.  `getStartTime` become `startTime` and `isEven` become `even`.

The next thing is a `BeanConverter` class.  It uses `LabelledBeanGeneric` and `LabelledGeneric`
to convert between beans and case classes.

```Scala
scala> case class Bar(a: Int, b: String)
defined class Bar

scala> val conv = BeanConverter[Foo, Bar]
conv: me.limansky.beanpuree.BeanConverter[Foo,Bar] = me.limansky.beanpuree.BeanConverter$$anon$1@4eae0bc5

scala> conv.beanToProduct(foo)
res3: Bar = Bar(5,changed)

scala> conv.productToBean(Bar(15, "bar"))
res4: Foo = Foo(15, "bar")
```

`BeanConverter` doesn't care about fields order, but requires to have all the
same fields with the same types.  It also mean that if the bean uses Java
numeric classes (like java.lang.Integer), the case class also should have the
field with Java class.

[shapeless]: http://github.com/milessabin/shapeless
