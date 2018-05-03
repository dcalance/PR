import javax.mail.{Folder, Store}

class StoreStruct(val store : Store, var currentPage : Int, var currentFolder : Folder, var mailsNumber : Int)
