# mux

Multiplexing repls.

## Usage

`mux` upgrades a repl to a multiplexed one. It takes two arguments `accept` (defaults to `clojure.core.server`) and `pipe-size` (defaults to 16384 chars).

## Protocol

The client has full ownership over the multiplexed channels and is reponsible for assigning and managing channel ids.

When a peer receives a `[id "content"]` message it appends `content` to the input stream of the channel identified by `id` -- if the peer is the server then it creates the channel it if doesn't exist.

When a peer receives a `[id nil]` message it closes the input stream of the channel identified by `id`.

## License

Copyright © 2017 Christophe Grand

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
