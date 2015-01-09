# doobie-remotely
Very brief demo of [doobie](https://github.com/tpolecat/doobie) hooked up to [remotely](https://github.com/oncue/remotely).

To run this you'll need the doobie and remotely tips, and a database with the doobie world db set up. Probably not worth it.

```
> run
[info] Updating {file:/Users/rnorris/Scala/doobie-remotely/}doobie-remotely...
[info] Resolving org.fusesource.jansi#jansi;1.4 ...
[info] Done updating.
[info] Running demo.WorldMain 
[server] NEGOTIATION - channel connected with /127.0.0.1:58895
[server] NEGOTIATION - creating queue with /127.0.0.1:58895
[server] NEGOTIATION - closing queue with /127.0.0.1:58895
[server] ----------------
[server] header: Map()
[server] trace: 461d496d-843b-4264-a533-b957a66fc60e
[server] request: pop(France)
[server] result: \/-(\/-(59225700))
[server] duration: 242504 microseconds
[client] ----------------
[client] header: Map()
[client] trace: 461d496d-843b-4264-a533-b957a66fc60e
[client] request: pop(France)
[client] result: \/-(\/-(59225700))
[client] duration: 399769 microseconds
[server] NEGOTIATION - creating queue with /127.0.0.1:58895
[server] NEGOTIATION - closing queue with /127.0.0.1:58895
[server] ----------------
[server] header: Map()
[server] trace: 75c5d396-d742-4c3e-adb7-11ce1c65cdc0
[server] request: pop(Chickenbutt)
[server] result: \/-(-\/(No such country: Chickenbutt))
[server] duration: 15018 microseconds
[client] ----------------
[client] header: Map()
[client] trace: 75c5d396-d742-4c3e-adb7-11ce1c65cdc0
[client] request: pop(Chickenbutt)
[client] result: \/-(-\/(No such country: Chickenbutt))
[client] duration: 25067 microseconds
[server] NEGOTIATION - creating queue with /127.0.0.1:58895
[server] NEGOTIATION - closing queue with /127.0.0.1:58895
[server] ----------------
[server] header: Map()
[server] trace: 550b54b0-696f-49ff-8b51-a42badab557e
[server] request: pop(Canada)
[server] result: \/-(\/-(31147000))
[server] duration: 8747 microseconds
[client] ----------------
[client] header: Map()
[client] trace: 550b54b0-696f-49ff-8b51-a42badab557e
[client] request: pop(Canada)
[client] result: \/-(\/-(31147000))
[client] duration: 16437 microseconds
```
