function foo(arg) {
	console.log('Maj\'om');
	var test = "foo",
		test1 = "foo",
		longString = "some longer string",
		longVar1 = longString,
		longVar2 = longString;
	console.log(test, test1, longVar1, longVar2);
	return 10;
}
