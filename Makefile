.PHONY: compile run clean help test

# Directory configuration
SRC_DIR := src
BIN_DIR := bin
MAIN_CLASS := chess.Main
TEST_CLASS := TestBotGame

# Compiler settings
JAVAC := javac
JAVA := java
JFLAGS := -d $(BIN_DIR) -sourcepath $(SRC_DIR)

# Default target
help:
	@echo "AFK2 Console Chess - Makefile Commands"
	@echo "======================================="
	@echo "  make compile    - Compile all Java source files"
	@echo "  make run        - Compile and run the chess game"
	@echo "  make clean      - Remove all compiled files"
	@echo "  make test       - Compile and run bot integration tests"
	@echo "  make help       - Show this help message"
	@echo ""
	@echo "Quick Start: make run"

# Compile all Java source files
compile:
	@echo "Compiling Java source files..."
	@mkdir -p $(BIN_DIR)
	@$(JAVAC) $(JFLAGS) $(SRC_DIR)/chess/**/*.java
	@echo "✓ Compilation complete"

# Run the main chess game
run: compile
	@echo "Starting AFK2 Console Chess..."
	@$(JAVA) -cp $(BIN_DIR) $(MAIN_CLASS)

# Run bot integration tests
test: compile
	@echo "Running bot integration tests..."
	@$(JAVAC) -d $(BIN_DIR) -cp $(BIN_DIR) $(TEST_CLASS).java
	@$(JAVA) -cp $(BIN_DIR) TestBotGame

# Clean compiled files
clean:
	@echo "Cleaning compiled files..."
	@rm -rf $(BIN_DIR)
	@echo "✓ Clean complete"

# Alias for running
.PHONY: play
play: run
