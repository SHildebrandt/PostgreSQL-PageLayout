PostgreSQL-PageLayout
=====================

This tool provides utilities to create html visualizations of Pages in PostgreSQL. The project is still under development, but I think the current status is already worth to be published here. It was started during the seminar "PostgreSQL Inside/Out" at the University of Tuebingen in Germany.


### Example output

You can find some (more or less) up-to-date examples in the output directory.


### Usage

You can run this tool easily with SBT. Just clone or download this repository to your system and run SBT in the top folder.

Example usage:
```
run -d booktown -t authors -n 0 -f output/booktown.html -u postgres -p postgres
```

The arguments `-d` (database), `-t` (table), `-n` (page number) and `-f` (output file name) are required.
Execute ```run --help``` for more detailed information.
