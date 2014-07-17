# JsAnalyser

A javascript analyser to find optimisable code...

## Usage

```
> make
> cat someJavascriptFile.js | ./run

```

## Example

sample.js file:

```
function foo(arg) {
	console.log('Maj\'om');
	var test = "foo",
		test1 = "foo",
		longVar1 = "some longer string",
		longVar2 = "some longer string";
	console.log(test, test1, longVar1, longVar2);
	return 10;
}
```

sample-min.js file (minimised with YUI compressor):

```
function foo(a){console.log("Maj'om");var e="foo",b="foo",d="some longer string",c="some longer string";console.log(e,b,d,c);return 10};
```

Run of jsAnalyser:

```
> cat sample-min.js | ./run
Report

Strings:
Maj'om: 1 occurence(s)
        Non optimisable
foo: 2 occurence(s)
        Non optimisable
some longer string: 2 occurence(s)
        Optimisable

Numerical values:
10: 1 occurence(s)
        Non optimisable

Tokens:
a: 1 occurence(s)
b: 2 occurence(s)
c: 2 occurence(s)
console: 2 occurence(s)
d: 2 occurence(s)
e: 2 occurence(s)
foo: 1 occurence(s)
function: 1 occurence(s)
log: 2 occurence(s)
return: 1 occurence(s)
var: 1 occurence(s)
```

## What to do next?

From the report, the Javascript file can be updated to be optimised.

For instance, in the example, it is said that the string "some longer string"
has been found twice and can be optimised. An according change could be to store
the duplicated string in a variable and use the variable instead of the string:

```
diff --git a/sample.js b/sample.js
index ded3698..0c95107 100644
--- a/sample.js
+++ b/sample.js
@@ -2,8 +2,9 @@ function foo(arg) {
        console.log('Maj\'om');
        var test = "foo",
                test1 = "foo",
-               longVar1 = "some longer string",
-               longVar2 = "some longer string";
+               longString = "some longer string",
+               longVar1 = longString,
+               longVar2 = longString;
        console.log(test, test1, longVar1, longVar2);
        return 10;
 }
```

Which creates the following minimised version:

```
function foo(a){console.log("Maj'om");var f="foo",b="foo",e="some longer string",d=e,c=e;console.log(f,b,d,c);return 10};
```

And here is the analyser report then:

```
> cat sample-min.js | ./run
Report

Strings:
Maj'om: 1 occurence(s)
        Non optimisable
foo: 2 occurence(s)
        Non optimisable
some longer string: 1 occurence(s)
        Non optimisable

Numerical values:
10: 1 occurence(s)
        Non optimisable

Tokens:
a: 1 occurence(s)
b: 2 occurence(s)
c: 2 occurence(s)
console: 2 occurence(s)
d: 2 occurence(s)
e: 3 occurence(s)
f: 2 occurence(s)
foo: 1 occurence(s)
function: 1 occurence(s)
log: 2 occurence(s)
return: 1 occurence(s)
var: 1 occurence(s)
```

All the strings are optimised.
