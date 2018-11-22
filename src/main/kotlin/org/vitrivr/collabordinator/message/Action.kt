package org.vitrivr.collabordinator.message

enum class Action {

    ADD, //add ids to list (server <--> client)
    REMOVE, //remove ids from list (server <--> client)
    LIST, //get entire current list (server <--- client)
    CLEAR //clear entire list (server <--> client)

}