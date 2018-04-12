# Contributing

Feel free to contribute any way you want. Whatever you feel is missing or "wrong", just fix it. (Before embarking on a significant amount of work it's a good idea bring up a discussion regarding what you plan to do.)

One very good way to contribute is to supply test cases that verify that the functionality you use works as expected. The ojAlgo code base is often refactored. Having tests specific to your use case can be very beneficial (for you) in assuring future releases.

...or have a look at the list of [open issues](https://github.com/optimatika/ojAlgo/issues) to see if there is something you can help out with.

Please use the develop branch rather than master!

### Existing Tests

The already existing tests needs some attention as well:

1. @Disabled  Essentially there should be no disabled tests. Rather than disabled they may be tagged "unstabe" or "slow". The only tests that potentially could be permanently annnnotated as disabled are some difficult optimisation problems. Find a test that is disbaled, and figure out why it is. Then either fix the problem or tag the "unstabe" or "slow" rather than leave it disabled.
2. @Tag("unstable") There are some tests that sometimes fail due to randomly generated input. The solution here is typically NOT to set a fixed seed on the random number generator (or to otherwise "fix" the input). Instead the solution is to find the root cause of the test failure. When the tests fail - what happened? In many cases the problem seems to be related to complex number arithmatics. When/if you find the root problem, then create a separate test for that, and solve that problem.
3. @Tag("slow") Is it possible to reliably test the same thing faster?
