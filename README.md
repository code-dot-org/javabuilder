# java-ide
In-browser Java IDE


# Prerequisites
### Ruby 2.7.4
You need to install Ruby 2.7.4 to build and/or run the Ruby lambda functions (`javabuilder-authorizer` and `api-gateway-routes`).
[rbenv](https://github.com/rbenv/rbenv) is a useful tool for installing Ruby 
and managing multiple versions. Follow the instructions [here](https://github.com/rbenv/rbenv#installing-ruby-versions) to
use rbenv to install a new Ruby version. You may need to also install [ruby-build](https://github.com/rbenv/ruby-build#readme) to get the latest Ruby
versions. The `.ruby-version` file sets the local Ruby version for javabuilder to be 2.7.4.
#### MacBook M1 chip-specific setup
If you have a new MacBook with the M1 chip, you may have to specify that you want to install using Rosetta 2 via the following:
```
arch -x86_64 rbenv install 2.7.4
```
It also may be helpful to update your homebrew and a couple of associated packages beforehand:
```
brew update
brew upgrade rbenv ruby-build
```