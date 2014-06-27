#!/bin/bash

expect -c "
    set timeout -1
    spawn $@

    expect {
        \"Do you accept the license\" {
            send \"y\n\"
            exp_continue
        }
        Downloading {
            exp_continue
        }
        Installing {
            exp_continue
        }
    }
"

