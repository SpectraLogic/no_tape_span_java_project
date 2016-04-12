No Tape Spanning
================

This cli application will create a data policy with tape spanning
disabled.  This means that a file must be under 1TB and it will be
put onto a single tape, rather than spanning across several.

## Usage

Extract the application from the pre-build tar or zip file located in
the
[releases](https://github.com/SpectraLogic/no_tape_span_java_project/releases).

Then run on linux:

```bash

$ no_tape_span_java_project endpoint access_id secret_key user_id [storage_domain_id]

```

Or on windows:

```bat

> no_tape_span_java_project.bat endpoint access_id secret_key user_id [storage_domain_id]

```

The application will create a data policy called, `no_tape_span`, and will assign it as the default data policy for the user passed in. The `user_id` can either be the ID or the name of the user.

You can optionally specify the storage domain to use.  If there is more than one storage domain, then the application will print a list of all the available storage domains that a user can choose from, then they must re-run the application specifying which storage domain to use.  If there is only one storage domain, it will be used.
