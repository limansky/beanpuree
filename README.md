BeanPurée
=========

**BeanPurée** is a middle layer between JavaBeans and [shapeless][shapeless].

> NOTE: The library is in active development stage. So the API might be changed.

[![Build Status](https://travis-ci.com/limansky/beanpuree.svg?branch=master)](https://travis-ci.com/limansky/beanpuree)
[![Join the chat at https://gitter.im/limansky/beanpuree](https://badges.gitter.im/limansky/beanpuree.svg)](https://gitter.im/limansky/beanpuree?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![codecov](https://codecov.io/gh/limansky/beanpuree/branch/master/graph/badge.svg)](https://codecov.io/gh/limansky/beanpuree)
[![Maven Central](https://img.shields.io/maven-central/v/me.limansky/beanpuree_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/me.limansky/beanpuree_2.12)

## Motivation

Even though Scala is compatible with Java, the languages are different, and the
coding styles are different too.  In Scala we prefer to use immutable case classes,
but in Java world mutable JavaBeans are common building blocks.  Moreover, many
Scala libraries provide API which requires case classes (e.g. different serializers).
As result, we need to have similar model classes for Java and Scala.  This library
helps to convert data between JavaBeans and case classes.

## Usage

BeanPurée is available for Scala 2.10, 2.11, 2.12 and 2.13. You can add it to your project
adding in `build.sbt`

```Scala
libraryDependencies += "me.limansky" %% "beanpuree" % "0.5"
```

If you'd like to use development version:

```Scala
resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies += "me.limansky" %% "beanpuree" % "0.6-SNAPSHOT"
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

`JavaTypeMapper[J, S]` provides converters for different Java and Scala classes.
There are converter instances from Java numeric classes to corresponding Scala ones.
It also can convert nullable value to `Option`.  You can convert `HList`s of convertable
values as well.

```Scala
scala> type JavaType = Integer :: String :: java.lang.Long :: HNil
defined type alias JavaType

scala> type ScalaType = Int :: String :: Option[Long] :: HNil
defined type alias ScalaType

scala> val m = JavaTypeMapper[JavaType, ScalaType]
m: me.limansky.beanpuree.JavaTypeMapper[JavaType,ScalaType] = me.limansky.beanpuree.JavaTypeMapper$$anon$1@41bbc4c4

scala> m.javaToScala(6 :: "test" :: null :: HNil)
res0: ScalaType = 6 :: test :: None :: HNil

scala> m.scalaToJava(42 :: null :: Some(66l) :: HNil)
res1: JavaType = 42 :: null :: 66 :: HNil
```

The next thing is a `BeanConverter` and `StrictBeanConverter` classes.  They use
`LabelledBeanGeneric` and `LabelledGeneric` to convert between beans and case classes.

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

The converters doesn't care about fields order.  The difference between these two
classes is that `StrictBeanConverter` requires the same fields of  converting classes
having the same types.  It means that if the bean uses Java
numeric classes (like java.lang.Integer), the case class also should have the
field with Java class.

`BeanConverter` is more intelligent.  It uses `JavaTypeMapper` to convert field types.
You should be careful using it.  For example if you have an Integer field in Java class
and Int in Scala, you might get a NullPointerException if the value is null.  Use `Option[Int]`
to make it safe.

[shapeless]: http://github.com/milessabin/shapeless
