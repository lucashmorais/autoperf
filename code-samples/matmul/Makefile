	LIBS=/usr/local/share/CBLAS_64/lib/libcblas.a,/usr/local/share/BLAS_64/blas_LINUX.a,-lg2c
	INC=/usr/local/share/CBLAS/src/
	OPTIONS=-Wl,-m64 -Wc,-m64 -O3


matmul: matmul.c
	mcc -o matmul matmul.c -Wl,$(LIBS) -I$(INC) $(OPTIONS) 

