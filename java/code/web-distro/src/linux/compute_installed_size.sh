#!/bin/bash

du -k -c $1 | tail -n 1 | cut  -f 1