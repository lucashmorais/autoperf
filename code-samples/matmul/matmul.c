#include <errno.h>
#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/time.h>
#include <time.h>
#include <cblas.h> // COMMENT THIS FOR NOT USE CBLAS

#define BSIZE 64

//Function for printing memory reads.
void printmr(long int pos)
{
	printf("R: %lx\n", pos);
}

//Function for printing memory writes.
void printmw(long int pos)
{
	printf("W: %lx\n", pos);
}

//Function for printing memory writes/reads.
void printmb(long int pos)
{
	printmr(pos);
	printmw(pos);
}

//Function for printing memory reads over specific region
void printmrd(long int base, long int size, int stride, char * fname)
{
	int i;

	if (fname != NULL) printf("%s\n", fname);
	for (i = 0; i < size * stride; i++)
		printmr(base + i);
}

//Function for printing memory writes over specific region
void printmwd(long int base, long int size, int stride, char * fname)
{
	int i;

	if (fname != NULL) printf("%s\n", fname);
	for (i = 0; i < size * stride; i++)
		printmw(base + i);
}

//Function for printing memory writes/reads over specific region
void printmbd(long int base, long int size, int stride, char * fname)
{
	if (fname != NULL) printf("%s\n", fname);
	printmrd(base, size, stride, NULL);	
	printmwd(base, size, stride, NULL);	
}

/*// CODE WITHOUT CBLAS

#pragma css task input(A[NB][NB], B[NB][NB], NB) inout(C[NB][NB])
void matmul(float  *A, float *B, float *C, unsigned long NB)
{
  int i, j, k, I;
  float tmp;
  for (i = 0; i < NB; i++)
  {
    I=i*NB;
    for (j = 0; j < NB; j++)
    {

      tmp=C[I+j];
      for (k = 0; k < NB; k++)
      {
        tmp+=A[I+k]*B[k*NB+j];
      }
      C[I+j]=tmp;
    }
  }
}




*/

//--------------------------------------------------------------

#pragma css task input(A[NB][NB], B[NB][NB], NB) inout(C[NB][NB])
void matmul(float  *A, float *B, float *C, unsigned long NB)
{
unsigned char TR='T', NT='N';
float DONE=1.0;

    printmrd((long int) A, (long int) NB * NB, sizeof(float), "matmul");
    printmrd((long int) B, (long int) NB * NB, sizeof(float), NULL);
    printmbd((long int) C, (long int) NB * NB, sizeof(float), NULL);
    cblas_sgemm(
        CblasRowMajor,
        CblasNoTrans, CblasNoTrans,
        NB, NB, NB,
        1.0, A, NB,
              B, NB,
         1.0, C, NB);

}

//----------------------------------------------------------------------------------------------


void compute(struct timeval *start, struct timeval *stop, unsigned long NB, unsigned long DIM, float *A[DIM][DIM], float *B[DIM][DIM], float *C[DIM][DIM])
{
  #pragma css start 
  gettimeofday(start,NULL);
  
  for (unsigned long i = 0; i < DIM; i++)
    for (unsigned long j = 0; j < DIM; j++)
      for (unsigned long k = 0; k < DIM; k++)
        matmul (A[i][k], B[k][j], C[i][j], NB);

  #pragma css finish
  gettimeofday(stop,NULL);
}

//--------------------------------------------------------------------------------


float **A;
float **B;
float **C;

void init(unsigned long argc, char **argv, unsigned long * N_p, unsigned long * DIM_p);


int
main(int argc, char *argv[])
{
  // local vars
  unsigned long NB, N, DIM;
  
  struct timeval start;
  struct timeval stop;
  unsigned long elapsed;

  // application inicializations
  init(argc, argv, &N, &DIM);

  // compute with CellSs
  compute(&start, &stop,(unsigned long) BSIZE, DIM, (void *)A, (void *)B, (void *)C);

  elapsed = 1000000 * (stop.tv_sec - start.tv_sec);
  elapsed += stop.tv_usec - start.tv_usec;
// time in usecs
  printf ("%lu;\t", elapsed);
// performance in MFLOPS
  printf("%lu\n", (unsigned long)((((double)N)*((double)N)*((double)N)*2)/elapsed));

	return 0;
}


static void convert_to_blocks(unsigned long NB,unsigned long DIM, unsigned long N, float *Alin, float *A[DIM][DIM])
{
  for (unsigned long i = 0; i < N; i++)
  {
    for (unsigned long j = 0; j < N; j++)
    {
      A[i/NB][j/NB][(i%NB)*NB+j%NB] = Alin[j*N+i];
    }
  }

}

void fill_random(float *Alin, int NN)
{
  int i;
  for (i = 0; i < NN; i++)
  {
    Alin[i]=((float)rand())/((float)RAND_MAX);
  }
}


void slarnv_(unsigned long *idist, unsigned long *iseed, unsigned long *n, float *x);

void
init (unsigned long argc, char **argv, unsigned long * N_p, unsigned long * DIM_p)
{
  unsigned long ISEED[4] = {0,0,0,1};
  unsigned long IONE=1;
  unsigned long DIM;
  char UPLO='n';
  float FZERO=0.0;

  if (argc==2)
  {
    DIM=atoi(argv[1]);
  }
  else
  {
    printf("usage: %s DIM\n",argv[0]);
    exit(0);
  }

  // matrix init
  unsigned long N=BSIZE*DIM;
  unsigned long NN=N*N;

  *N_p=N;
  *DIM_p=DIM;

  // linear matrix
  float *Alin = (float *) malloc(NN * sizeof(float));
  float *Blin = (float *) malloc(NN * sizeof(float));
  float *Clin = (float *) malloc(NN * sizeof(float));

  // fill the matrix with random values
  srand(0);
  fill_random(Alin,NN);
  fill_random(Blin,NN);
  for (int i=0; i < NN; i++)
    Clin[i]=0.0;

  A = (float **) malloc(DIM*DIM*sizeof(float *));
  B = (float **) malloc(DIM*DIM*sizeof(float *));
  C = (float **) malloc(DIM*DIM*sizeof(float *));
  
  for (unsigned long i = 0; i < DIM*DIM; i++)
  {
     A[i] = (float *) malloc(BSIZE*BSIZE*sizeof(float));
     B[i] = (float *) malloc(BSIZE*BSIZE*sizeof(float));
     C[i] = (float *) malloc(BSIZE*BSIZE*sizeof(float));
  }
  convert_to_blocks(BSIZE,DIM, N, Alin, (void *)A);
  convert_to_blocks(BSIZE,DIM, N, Blin, (void *)B);
  convert_to_blocks(BSIZE,DIM, N, Clin, (void *)C);

  free(Alin);
  free(Blin);
  free(Clin);

}

