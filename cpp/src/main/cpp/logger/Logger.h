#ifndef MMORPG_LOGGER_H
#define MMORPG_LOGGER_H

#include <string>


class Logger {
public:
    Logger(const char *name);

    ~Logger();

    void trace(const std::string, const std::string... arguments);

    void debug(const std::string, const std::string... arguments);

    void info(const std::string, const std::string... arguments);

    void warn(const std::string, const std::string... arguments);

    void error(const std::string, const std::string... arguments);

    void print(const std::string color, const std::string, const std::string... arguments);

protected:
    std::string format(const std::string string, const std::string... arguments);

    const char *name;
};


#endif //MMORPG_LOGGER_H
