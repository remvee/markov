# markov-chain

A Clojure library to generate data using Markov chain probabilities.

## Usage

Analyse some text:

> (def state (analyse-text (slurp (java.net.URL. "https://www.gutenberg.org/cache/epub/10/pg10.txt"))))
  
Generate some:

> (generate-text state)

## License

Copyright © 2019 R.W. van 't Veer

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
