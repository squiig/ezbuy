#
# Local .bashrc for EzBuy
# author: cerrealic

source scripts/bump.sh

alias run='cd server && start start.bat && cd ..'
alias bl='./scripts/build.sh EzBuy'
alias bll='bl && run'
alias bp='bump'
