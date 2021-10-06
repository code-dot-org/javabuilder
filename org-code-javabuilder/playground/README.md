# Playground Mini-App
This portion of Javabuilder contains student-facing API and internal code 
that supports the Playground mini-app in Javalab.

The Playground is a simple interface for creating interactive experiences in Java. 
The scenarios are limited to simple games like tic-tac-toe, Checkers, or Connect Four. 
The Playground has a similar presentation to the Theater, but the Playground does not 
have a script. Rather, the Playground simply presents images & text on a “board” that 
can also play sounds.

It is worth noting that the Playground is intended to be treated as a prototype, 
even after its launch as part of the pilot curriculum. This means that, while the feature 
is intended to be used by teachers and supported in the pilot curriculum and SY22-23, 
it may be significantly or entirely redesigned in the future (starting SY23-24).

### Student-Facing API
The student-facing API in this package consists of 
- [`Board.java`](https://github.com/code-dot-org/javabuilder/blob/main/org-code-javabuilder/playground/src/main/java/org/code/playground/Board.java) (public methods only)
- [`ClickableImage.java`](https://github.com/code-dot-org/javabuilder/blob/main/org-code-javabuilder/playground/src/main/java/org/code/playground/ClickableImage.java)
- [`ImageItem.java`](https://github.com/code-dot-org/javabuilder/blob/main/org-code-javabuilder/playground/src/main/java/org/code/playground/ImageItem.java)
- [`TextItem.java`](https://github.com/code-dot-org/javabuilder/blob/main/org-code-javabuilder/playground/src/main/java/org/code/playground/TextItem.java)
- [`PlaygroundException.java`](https://github.com/code-dot-org/javabuilder/blob/main/org-code-javabuilder/playground/src/main/java/org/code/playground/PlaygroundException.java)
- [`Playground.java`](https://github.com/code-dot-org/javabuilder/blob/main/org-code-javabuilder/playground/src/main/java/org/code/playground/Playground.java) (only for accessing `Board` singleton)

Students create instances of `ClickableItem`, `TextItem` and `ImageItem`, and manipulate them via
public methods in `Board.java`. Students also start and stop their game via `start()` and `end()` methods
in `Board.java`. A singleton instance of `Board` is available as a static member in `Playground.java`
and is not meant to be instantiated directly by students. `Board` may also throw `PlaygroundException`s
which are meant to be handled by students.

### Internal Code
The code in this package is also responsible for maintaining the state of the
Playground game, consuming updates from Javalab, and sending updates back to Javalab.
`Board.java` primarily manages game state and consumes updates, and messages to Javalab are
routed through `PlaygroundMessageHandler.java`. In addition, `PlaygroundSignalKey.java`
and `PlaygroundExceptionKeys.java` contain constants for message and exception types
respectively that are sent to Javalab.

### Interactions & Messaging

Playground games are driven by click interactions. `ClickableItem.java` is an abstract
class whose subclasses need to override an `onClick()` method, which is called when
the respective item is clicked. As such, messages sent by Javalab to Javabuilder indicate
which specific object has been clicked on, and messages sent by Javabuilder to Javalab
indicate what updates to make to items on the Playground (such as adding or removing items, 
updating item properties, setting a background image, or playing sounds). On receiving a
message from Javalab indicating that an object has been clicked, Javabuilder invokes the item's
`onClick()` method and dispatches any resulting updates to Playground items to Javalab. Javabuilder is not
responsible for managing view presentation, detecting click events, or determining which item
was clicked; Javalab manages all of these responsibilities on the client.

### Resources & Further Reading

#### Documentation
- [Playground Technical Design Document](https://docs.google.com/document/d/1Moo2s5EXZRp5rMg1VW9jlOqs_GeMN5yjU8FJgoqOEMk/edit?usp=sharing)
- [CSA Capabilities Spec](https://docs.google.com/document/d/14S47uuVF-hzxYeiw4ap-WqlN4A8ctUOypPNTwRKGh6c/edit#heading=h.6v77hisrc3uw)

#### Links
- [Javalab Playground All the Things Level](https://studio.code.org/s/allthethings/lessons/44/levels/9)
- [code-dot-org Javalab directory](https://github.com/code-dot-org/code-dot-org/tree/staging/apps/src/javalab)