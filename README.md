call example
└> env ls -1 ~/steamapps/appmanifest_* | ./acf-unused.clj ~/steamapps/common/ debug

└> env ls -1 ~/steamapps/appmanifest_* | ./acf-diff.clj $PWD/test/files/appmanifest_* -d -n
