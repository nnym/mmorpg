#include "Logger.h"
#include <iostream>

using namespace std;

Logger::Logger(const char *name) : name(name) {

}

Logger::~Logger() {
    delete this->name;
}

void Logger::trace(const string, const string... arguments) {
    this->print(WHITE, string, arguments);
}

void Logger::debug(const string, const string... arguments) {
    this->print(BOLDBLACK, string, arguments);
}

void Logger::info(const string, const string... arguments) {
    this->print(CYAN, string, arguments);
}

void Logger::warn(const string, const string... arguments) {
    this->print(ORANGE, string, arguments);
}

void Logger::error(const string, const string... arguments) {
    this->print(RED, string, arguments);
}

void Logger::print(string color, string, const string... arguments) {
    cout << color << this->format(string, arguments) << std::endl;
}

string Logger::format(const string string, const string... arguments) {
    for (const string : arguments) {
        string.replace()
    }
}
