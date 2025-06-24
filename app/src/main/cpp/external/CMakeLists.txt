project(external)

macro(SET_OPTION option value)
    set(${option} ${value} CACHE INTERNAL "" FORCE)
endmacro()

SET_OPTION(FMT_INSTALL OFF)
add_subdirectory(fmt)


