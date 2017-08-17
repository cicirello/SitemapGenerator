# SitemapGenerator
Command line utility for generating an xml sitemap for a website whose code is maintained via git.  It walks the directory hierarchy of the site, and uses the command line git command to find the last commit dates for use as last modification dates in the sitemap.

Copyright (C) 2017 Vincent A. Cicirello.
http://www.cicirello.org/

Configure the sitemap generator via the config.txt file, which supports the following:
```  GIT_ROOT: C:\FullPathToLocalGitRepository\user.github.io
  GIT_EXEC: C:\FullPathToGitCommand\cmd\git
  PAGE_ROOT: http://www.YourWebAddressHere.org [Note: no trailing slash]
  INCLUDE_EXT: html pdf [And any other file extensions you want in the sitemap space separated]
  EXCLUDE_DIR: images [Space separated list of any directories you want to exclude from sitemap, images is just an example]
  EXCLUDE_FILE: 404.html [Space separated list of any specific files you want excluded from sitemap]
```

See the provided sample config.txt.

config.txt must contain the `GIT_ROOT`, `GIT_EXEC`, and `PAGE_ROOT` lines.

If config.txt does not contain an `INCLUDE_EXT` declaration, then nothing will be entered into
your sitemap, not even .html files.  You may have multiple `INCLUDE_EXT` declarations, rather than one
with a list of extensions.

A note on index.html files: index.html files will appear in the sitemap via their
folder name only.  For example, it won't generate a sitemap entry: `http://www.mysite.com/blog/index.html`
Instead, it will generate the sitemap entry: `http://www.mysite.com/blog/`

Both `EXCLUDE_DIR` and `EXCLUDE_FILE` are optional.  `EXCLUDE_DIR` is used to specify entire directories
that you don't want in your sitemap.  You may have one declaration with a list of directories, or
you may have multiple declarations.  The sitemap generator will simply skip over any directories in 
that list when it walks your site's hierarchy.  Likewise, `EXCLUDE_FILE` is used to specify individual
files that you want excluded from the sitemap, such as the 404.html in the example.  All matching files
will be excluded, so use full path relative to root of site if there are same named files in multiple
directories and you only want to exclude one of them.

