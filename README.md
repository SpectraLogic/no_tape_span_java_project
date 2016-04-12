No Tape Spanning
================

This cli application will create a data policy with tape spanning
disabled.  This means that a file must be under 1TB and it will be
put onto a single tape, rather than spanning across several.

## Usage

Extract the application from the pre-build tar or zip file located in
the
[releases](https://github.com/SpectraLogic/no_tape_span_java_project/releases).

Then run:
''' bash

$ no_tape_span_java_project endpoint access_id secret_key user_id [storage_domain_id]

'''
