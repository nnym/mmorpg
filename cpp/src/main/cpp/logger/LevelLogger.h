#ifndef MMORPG_LEVELLOGGER_H
#define MMORPG_LEVELLOGGER_H

#include <string>


class LevelLogger {
public:
    LevelLogger(const std::string identifier);

protected:
    std::string identifier;
};

static const LevelLogger TRACE = LevelLogger("TRACE");
static const LevelLogger DEBUG = LevelLogger("DEBUG");
static const LevelLogger INFO = LevelLogger("INFO");
static const LevelLogger WARN = LevelLogger("WARN");
static const LevelLogger ERROR = LevelLogger("DEBUG");

#endif //MMORPG_LEVELLOGGER_H
