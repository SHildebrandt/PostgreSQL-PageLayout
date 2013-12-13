PostgreSQL-PageLayout
=====================

This tool provides utilities to create visualizations of Pages in PostgreSQL.

### Usage:

You can run this tool easily with SBT.

Example usage:
```
run -d booktown -t authors -n 0 -f output/booktown.html -u postgres -p postgres
```

The arguments `-d` (database), `-t` (table), `-n` (page number) and `-f` (output file name) are required.
Execute ```run --help``` for more detailed information.