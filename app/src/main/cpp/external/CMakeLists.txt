project(external)

macro(SET_OPTION option value)
    set(${option} ${value} CACHE INTERNAL "" FORCE)
endmacro()

SET_OPTION(DOBBY_GENERATE_SHARED OFF)
SET_OPTION(FMT_INSTALL OFF)


set(XZ_EMBEDDED_SOURCES
        xz/xz_crc32.c
        xz/xz_crc64.c
        xz/xz_dec_bcj.c
        xz/xz_dec_lzma2.c
        xz/xz_dec_stream.c
)

list(TRANSFORM XZ_EMBEDDED_SOURCES PREPEND xz-embedded/linux/lib/)

set(XZ_EMBEDDED_FLAGS
        -std=c89
        -DXZ_INTERNAL_CRC32=1
        -DXZ_USE_CRC64=1
        -DXZ_INTERNAL_CRC64=1
)
set(XZ_EMBEDDED_EXPORT_INCLUDES xz-embedded/linux/include/linux)
set(XZ_EMBEDDED_INCLUDES ${XZ_EMBEDDED_EXPORT_INCLUDES} xz-embedded/userspace)

add_library(xz_embedded STATIC ${XZ_EMBEDDED_SOURCES})
target_compile_options(xz_embedded PRIVATE)
target_include_directories(xz_embedded PUBLIC ${XZ_EMBEDDED_EXPORT_INCLUDES})
target_include_directories(xz_embedded PRIVATE ${XZ_EMBEDDED_INCLUDES})

link_libraries(xz_embedded)

OPTION(LSPLANT_BUILD_SHARED OFF)

#add_subdirectory(lsplant/lsplant/src/main/jni)
#add_subdirectory(dobby)
add_subdirectory(fmt)
#target_compile_definitions(fmt-header-only INTERFACE FMT_STATIC_THOUSANDS_SEPARATOR=1 FMT_USE_FLOAT=0 FMT_USE_DOUBLE=0 FMT_USE_LONG_DOUBLE=0)





