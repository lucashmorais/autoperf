#include <stdio.h>
#include <stdlib.h>

int main (int argc, char** argv)
{
	int i;
	int sum;

	printf("atoi(%s): %d\n", argv[1], atoi(argv[1]));

	for (sum = 0, i = 0; i < atoi(argv[1]); i++)
		sum += rand();
	
	printf("Random sum: %d\n", sum);

	return 0;
}
