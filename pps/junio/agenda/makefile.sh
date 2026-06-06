
# Compilador y flags
CC     = gcc
FLAGS  = -ansi -pedantic -Wall -Werror

# Directorios
SRC    = src
INC    = includes
BIN    = bin

# Ficheros
SRCS   = $(SRC)/main.c $(SRC)/agenda.c $(SRC)/contacto.c
TARGET = $(BIN)/AgendaMuestra

# Regla por defecto — compila todo de una vez
all: $(TARGET)

$(TARGET): $(SRCS)
	$(CC) $(FLAGS) -I$(INC) $(SRCS) -o $(TARGET)

# Limpia solo el ejecutable (no hay .o que borrar)
clean:
	rm -f $(TARGET)

re: clean all